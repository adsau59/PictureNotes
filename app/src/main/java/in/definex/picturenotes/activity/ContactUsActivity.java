package in.definex.picturenotes.activity;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import in.definex.picturenotes.BuildConfig;
import in.definex.picturenotes.R;
import in.definex.picturenotes.util.UtilityFunctions;

public class ContactUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        ((ImageView)findViewById(R.id.arrow_icon)).setImageDrawable(UtilityFunctions.makeIcon(this,"\uf078",20, Color.GRAY));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Spinner spinner = (Spinner)findViewById(R.id.spinner);
        String[] strings = new String[]{"Select Type of Message","Error Report", "Feature Request", "Suggestion", "Other"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, strings);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);


        final Context context = this;
        findViewById(R.id.submitButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "http://definex.in/api/picturenotes/contact";
                RequestQueue queue = Volley.newRequestQueue(context);

                final String email = ((EditText)findViewById(R.id.emailEt)).getText().toString().trim();
                final String name = ((EditText)findViewById(R.id.nameEt)).getText().toString().trim();
                String content = ((EditText)findViewById(R.id.emailEt)).getText().toString().trim();

                String errStr = "";
                String type = "";
                switch ((int) spinner.getSelectedItemId()){
                    case 0:
                        errStr += "Select Valid Option\n";
                        break;

                    case 1:
                        type = "error";
                        break;

                    case 2:
                        type = "feature";
                        break;

                    case 3:
                        type = "suggestion";
                        break;

                    case 4:
                        type = "other";
                        break;

                }

                if(email.isEmpty() || name.isEmpty() || content.isEmpty()) errStr += "Fill all fields";

                if(!email.isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
                    errStr += "Email is not valid";

                if(!errStr.isEmpty()){
                    Toast.makeText(context,errStr,Toast.LENGTH_LONG).show();
                    return;
                }

                content+="Version"+ BuildConfig.VERSION_CODE;

                final String fContent = content;
                final String fType = type;
                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                Log.d("Response",response);

                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    if(jsonObject.getInt("status") == 200)
                                        Toast.makeText(context, "sent response with id: " + jsonObject.getInt("id"), Toast.LENGTH_LONG).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }){
                    @Override
                    public String getBodyContentType() {
                        return "application/x-www-form-urlencoded; charset=UTF-8";
                    }

                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String,String> params = new HashMap<String, String>();
                        params.put("email",email);
                        params.put("name",name);
                        params.put("type", fType);
                        params.put("content",fContent);
                        return params;
                    }
                };
                // Add the request to the RequestQueue.
                queue.add(stringRequest);
                finish();
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}

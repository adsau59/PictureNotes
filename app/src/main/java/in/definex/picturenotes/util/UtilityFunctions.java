package in.definex.picturenotes.util;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.text.Layout;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import in.definex.picturenotes.BuildConfig;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;

/**
 * Created by adam_ on 01-12-2016.
 */

public class UtilityFunctions {

    public static Bitmap textAsBitmap(String text, Typeface typeface, float textSize, int textColor) {
        Paint paint = new Paint(ANTI_ALIAS_FLAG);
        paint.setTextSize(textSize);
        paint.setTypeface(typeface);
        paint.setColor(textColor);
        paint.setTextAlign(Paint.Align.LEFT);
        float baseline = -paint.ascent(); // ascent() is negative
        int width = (int) (paint.measureText(text) + 0.0f); // round
        int height = (int) (baseline + paint.descent() + 0.0f);
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(image);
        canvas.drawText(text, 0, baseline, paint);
        return image;
    }

    public static TextDrawable makeIcon(Context context, String iconCode, float size){
        return makeIcon(context, iconCode, size, Color.WHITE);

    }

    public static TextDrawable makeIcon(Context context, String iconCode){
        return makeIcon(context, iconCode, 35, Color.WHITE);
    }

    public static TextDrawable makeIcon(Context context, String iconCode, float size, int color){
        TextDrawable faIcon = new TextDrawable(context);
        faIcon.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
        faIcon.setTextAlign(Layout.Alignment.ALIGN_CENTER);
        faIcon.setTypeface(FontAwesomeManager.getTypeface(context, FontAwesomeManager.FONTAWESOME));
        faIcon.setText(iconCode);
        faIcon.setTextColor(color);
        return faIcon;
    }

    public static boolean getTutorialStatus(Context context){
        return context.getSharedPreferences("prefs",Context.MODE_PRIVATE).getBoolean("tutorialActive",true);
    }

    public static void setTutorialStatus(Context context, Boolean isActive){
        context.getSharedPreferences("prefs",Context.MODE_PRIVATE).edit().putBoolean("tutorialActive",isActive).apply();
    }

    public static boolean getPermisionCompleteStatus(Context context){
        return context.getSharedPreferences("prefs",Context.MODE_PRIVATE).getBoolean("permission",false);
    }

    public static void setPermisionStatus(Context context, Boolean done){
        context.getSharedPreferences("prefs",Context.MODE_PRIVATE).edit().putBoolean("permission",done).apply();
    }

    public static void checkUpdate(final Context context, final boolean showIsPositiveMessage){
        int versionCode = BuildConfig.VERSION_CODE;
        String url = "http://definex.in/api/picturenotes/updateinfo/"+versionCode;
        Log.d("sending request to ", url);


        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Log.d("Response",response);
                    final JSONObject jsonObject = new JSONObject(response);

                    if(jsonObject.getInt("status")==200){
                        if(jsonObject.getInt("isLatest") == 1) {

                            if (showIsPositiveMessage)
                                Toast.makeText(context, "Picture Notes is up to date", Toast.LENGTH_SHORT).show();

                        }else{
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);

                            String message = (jsonObject.getInt("isBackwardCompatible") == 0)?"Can't use Picture Notes without Update":"New Version of Picture Notes Available";

                            message+="\n("+jsonObject.getString("newVersionName")+")";

                            builder.setTitle("Update Available")
                                    .setMessage(message)
                                    .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            final String appPackageName = context.getPackageName(); // getPackageName() from Context or Activity object
                                            try {
                                                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                            }
                                            catch (android.content.ActivityNotFoundException anfe) {
                                                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
                                            }
                                        }
                                    }).setNegativeButton("Not Now", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    try {
                                        if(jsonObject.getInt("isBackwardCompatible") == 0)
                                            System.exit(0);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                                builder.show();
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        queue.add(stringRequest);
    }


    public static void checkNews(final Context context){
        int versionCode = BuildConfig.VERSION_CODE;
        String url = "http://definex.in/api/picturenotes/checkNews/"+versionCode;
        Log.d("sending request to ", url);


        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Log.d("Response",response);
                    final JSONObject jsonObject = new JSONObject(response);

                    if(jsonObject.getInt("status")==200){
                        if(jsonObject.getInt("alert") == 1){
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle(jsonObject.getString("title"))
                                    .setMessage(jsonObject.getString("message"))
                                    .setPositiveButton(jsonObject.getString("positive_text:"), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            try {
                                                switch (jsonObject.getString("positive_action")){
                                                    case "nothing":
                                                        break;
                                                    case "openPlayStore":
                                                        final String appPackageName = context.getPackageName(); // getPackageName() from Context or Activity object
                                                        try {
                                                            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                                        }
                                                        catch (android.content.ActivityNotFoundException anfe) {
                                                            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
                                                        }
                                                        break;
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    })
                                    .setNegativeButton(jsonObject.getString("negative_text"), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            try {
                                                switch (jsonObject.getString("positive_action")){
                                                    case "nothing":
                                                        break;
                                                    case "closeApp":
                                                        System.exit(0);
                                                        break;
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });

                            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialogInterface) {
                                    try {
                                        switch (jsonObject.getString("positive_action")){
                                            case "nothing":
                                                break;
                                            case "closeApp":
                                                System.exit(0);
                                                break;
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                            builder.show();
                        }
                    }

                    String string = jsonObject.getString("echo");

                    if(string==null || !string.isEmpty())
                        Toast.makeText(context, string, Toast.LENGTH_LONG).show();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        queue.add(stringRequest);
    }

    public static void setCopyMode(Context context, COPY_MODE mode){
        context.getSharedPreferences("prefs",Context.MODE_PRIVATE).edit().putInt("copyMode",mode.getVal()).apply();
    }

    public static COPY_MODE getCopyMode(Context context){
        return COPY_MODE.getMod(context.getSharedPreferences("prefs",Context.MODE_PRIVATE).getInt("copyMode",0));
    }

    public static void copyFile(String inputFileUrl, String outputFileUrl) {

        InputStream in;
        OutputStream out;
        try {

            //create output directory if it doesn't exist
            File outputFile = new File (outputFileUrl);
            if(!outputFile.getParentFile().exists())
                outputFile.getParentFile().mkdirs();

            in = new FileInputStream(inputFileUrl);
            out = new FileOutputStream(outputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file (You have now copied the file)
            out.flush();
            out.close();
            out = null;

        }  catch (FileNotFoundException fnfe1) {
            Log.e("tag", fnfe1.getMessage());
        }
        catch (Exception e) {
            Log.e("tag", e.getMessage());
        }

    }

    public enum COPY_MODE{
        NONE(0),
        COPY(1),
        MOVE(2);

        private int val;
        COPY_MODE(int val){
            this.val = val;
        }

        public int getVal() {
            return val;
        }

        public static COPY_MODE getMod(int val){
            for(COPY_MODE c : values()){
                if(c.getVal() == val)
                    return  c;
            }
            return NONE;
        }
    }

    public static void deleteImage(Context context,String filePath) {
        new File(filePath).delete();
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(filePath))));
    }

    public static void setFavDisturbed(Context context, Boolean bool){
        context.getSharedPreferences("prefs",Context.MODE_PRIVATE).edit().putBoolean("favDisturbed",bool).apply();
    }

    public static boolean getFavDisturbed(Context context){
        return context.getSharedPreferences("prefs",Context.MODE_PRIVATE).getBoolean("favDisturbed",false);
    }

}

package in.definex.picturenotes.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.zoom.ZoomableImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import in.definex.picturenotes.models.ImageData;
import in.definex.picturenotes.util.ImageLoader;
import in.definex.picturenotes.models.NoteModel;
import in.definex.picturenotes.R;
import in.definex.picturenotes.activity.ShowImageActivity;
import in.definex.picturenotes.util.UtilityFunctions;

/**
 * Created by adam_ on 27-11-2016.
 */

public class ShowImageRecyclerAdapter extends RecyclerView.Adapter<ShowImageRecyclerAdapter.MyViewHolder> {

    private LayoutInflater inflater;
    public List<ImageData> imageDatas;
    private Context context;
    public HashMap<Integer, Bitmap> bitmapHashMap;
    public Bitmap defaultBitmap;

    private Drawable shareIcon;
    private Drawable editIcon;
    private Drawable rotateIcon;
    private Drawable closeIcon;
    private Drawable deleteIcon;

    private NoteModel noteModel;

    public ShowImageRecyclerAdapter(Context context, List<ImageData>imageDatas, NoteModel noteModel){
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        updateData(imageDatas);
        defaultBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.defaut_img);

        this.noteModel = noteModel;

        shareIcon = UtilityFunctions.makeIcon(context, "\uf064");
        editIcon = UtilityFunctions.makeIcon(context, "\uf044",30);
        rotateIcon = UtilityFunctions.makeIcon(context, "\uf01e");
        closeIcon = UtilityFunctions.makeIcon(context, "\uf053");
        deleteIcon = UtilityFunctions.makeIcon(context, "\uF1F8");
    }


    public void updateData(List<ImageData> imageDatas){
        bitmapHashMap = new HashMap<>();
        this.imageDatas = imageDatas;
    }

    public void updateListData(List<ImageData> imageDatas){
        this.imageDatas = imageDatas;
        notifyDataSetChanged();
    }

    public void exchangeBitMaps(int pos1, int pos2){
        Bitmap t = bitmapHashMap.get(pos1);
        bitmapHashMap.put(pos1, bitmapHashMap.get(pos2));
        bitmapHashMap.put(pos2, t);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.image_grid_layout, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    boolean selectorOn =false;
    public void setSelector(boolean on){
        selectorOn = on;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        final int pos = position;


        if(selectorOn){
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imageDatas.get(pos).setSelected(!imageDatas.get(pos).isSelected());
                    holder.checkBox.setChecked(imageDatas.get(pos).isSelected());
                }
            });

        }else {
            holder.checkBox.setVisibility(View.INVISIBLE);
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openImage(v,pos);
                }
            });
        }

        holder.checkBox.setChecked(imageDatas.get(pos).isSelected());

        holder.checkBox.setOnClickListener(v -> imageDatas.get(pos).setSelected(holder.checkBox.isChecked()));

        if(holder.imageView != null) {
            holder.imageView.setImageBitmap(defaultBitmap);
            if (bitmapHashMap.get(position) == null)
                new ImageLoader(holder.imageView, this, context, imageDatas.get(pos).getName()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,imageDatas.get(position).getUrl(), String.valueOf(position));
            else {
                holder.imageView.setImageBitmap(bitmapHashMap.get(position));
            }
        }

    }

    public interface ActivityFuncs{
        void hideStatus();
        void showStatus();
    }

    ActivityFuncs activityFuncs;

    public void setActivityFuncs(ActivityFuncs activityFuncs) {
        this.activityFuncs = activityFuncs;
    }

    float rotation;
    //NOTEME OPEN IMAGES
    public void openImage(View anchorView, final int pos){

        activityFuncs.hideStatus();

        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        final View popupView = inflater.inflate( R.layout.view_image_layout, null );
        final PopupWindow popupWindow = new PopupWindow(popupView, RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT);

        rotation = 0;

        //((ShowImageActivity)context).onWindowFocusChanged(true);

        final ZoomableImageView imageView = (ZoomableImageView) popupView.findViewById(R.id.view_image);
        imageView.setImageBitmap(defaultBitmap);
        new Thread(){

            @Override
            public void run() {
                final Bitmap bm = urlToBitmap(imageDatas.get(pos).getUrl(),rotation);

                ((Activity)context).runOnUiThread(new Runnable(){

                    @Override
                    public void run() {
                        if(bm == null)
                            Toast.makeText(context, R.string.cant_find_image, Toast.LENGTH_LONG).show();
                        else
                            imageView.setImageBitmap(bm);

                        popupView.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                    }
                });
            }
        }.start();

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View v1 = popupView.findViewById(R.id.botRelativeLayout);
                View v2 = popupView.findViewById(R.id.topRelativeLayout);

                if(v1.getVisibility() == View.VISIBLE){
                    v1.setVisibility(View.INVISIBLE);
                    v2.setVisibility(View.INVISIBLE);
                }else{
                    v1.setVisibility(View.VISIBLE);
                    v2.setVisibility(View.VISIBLE);
                }
            }
        });



        //NOTEME SHARE BUTTON
        ImageView shareIv = (ImageView)popupView.findViewById(R.id.shareImageView);
        shareIv.setImageDrawable(shareIcon);
        shareIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/*");
                Uri uri = Uri.parse(imageDatas.get(pos).getUrl());
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.shared_via_space)+context.getString(R.string.app_name));

                Intent.createChooser(intent, context.getString(R.string.share_via));
                context.startActivity(intent);


            }
        });


        //NOTEME ROTATE BUTTON
        ImageView rotateIv = (ImageView) popupView.findViewById(R.id.rotateImageView);
        rotateIv.setImageDrawable(rotateIcon);
        rotateIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotation+=90;
                if(rotation>270)
                    rotation = 0;

                Bitmap bm = urlToBitmap(imageDatas.get(pos).getUrl(),rotation);
                if(bm == null)
                    bm = defaultBitmap;

                imageView.setImageBitmap(bm);
            }
        });

        //NOTEME CLOSE BUTTON
        final ImageView closeIv = ((ImageView)popupView.findViewById(R.id.closeImageView));
        closeIv.setImageDrawable(closeIcon);

        closeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        //NOTEME NAME
        final TextView textView = (TextView)popupView.findViewById(R.id.imageName);
        textView.setText(imageDatas.get(pos).getNumber()+". "+imageDatas.get(pos).getName());


        ImageView editIv = (ImageView)popupView.findViewById(R.id.editNameButton);
        editIv.setImageDrawable(editIcon);
        editIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
                final EditText editText = new EditText(context);
                builder.setTitle(R.string.change_name)
                        .setView(editText)
                        .setPositiveButton(R.string.apply, new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String newName = editText.getText().toString();

                                if(newName.length()<=10){
                                    imageDatas.get(pos).setName(newName);
                                    imageDatas.get(pos).update();
                                    textView.setText(newName);
                                }else{
                                    Toast.makeText(context, R.string.max_10_characters, Toast.LENGTH_SHORT).show();
                                }

                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();
            }
        });

        //NOTEME DELETE IMAGE STUFF
        ImageView deleteIv = (ImageView)popupView.findViewById(R.id.deleteImageView);
        deleteIv.setImageDrawable(deleteIcon);
        deleteIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View v1 = ShowImageActivity.getDeleteImageDialogeView(context);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.delete_images)
                        .setView(v1)
                        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //delete image from db
                                imageDatas.get(pos).delete();
                                noteModel.cachedShareNoteDisturbed(context);
                                UtilityFunctions.setFavDisturbed(context, true);
                                if(((CheckBox)v1.findViewById(R.id.deleteImagesCheckBox)).isChecked())
                                    UtilityFunctions.deleteImage(context, imageDatas.get(pos).getUrl());

                                imageDatas.remove(pos);

                                //adjust numbers of others
                                for(int i=0, num=1; i<imageDatas.size(); i++,num++) {
                                    imageDatas.get(i).setNumber(num);
                                    imageDatas.get(i).update();
                                }
                                //dismiss and refresh view
                                updateData(imageDatas);
                                notifyDataSetChanged();
                                popupWindow.dismiss();


                            }
                        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
            }
        });

        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable());
        popupWindow.showAtLocation(anchorView, Gravity.CENTER,
                0, 0);

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                activityFuncs.showStatus();
            }
        });

    }

    private Bitmap urlToBitmap(String path, float mRotation) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);

        if(bitmap == null)
            return null;


        /*int nh = (int) (bitmap.getHeight() * (512.0 / bitmap.getWidth()));
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 512, nh, true);*/

        Bitmap drawableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        Matrix matrix = new Matrix();
        matrix.postRotate(mRotation);
        return Bitmap.createBitmap(drawableBitmap, 0, 0, drawableBitmap.getWidth(), drawableBitmap.getHeight(), matrix, true);

    }

    @Override
    public int getItemCount() {
        return imageDatas.size();
    }

    public List<ImageData> selectedImages(){
        List<ImageData> list = new ArrayList<>();
        for(ImageData imageData : imageDatas){
            if(imageData.isSelected()){
                list.add(imageData);
            }
        }

        return list;
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        CheckBox checkBox;
        public MyViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            checkBox = (CheckBox) itemView.findViewById(R.id.imageCheckbox);
        }
    }


}

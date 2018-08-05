package in.definex.picturenotes.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import in.definex.picturenotes.models.BackupModel;
import in.definex.picturenotes.R;

/**
 * Created by adam_ on 04-12-2016.
 */

public class BackupRecyclerAdapter extends RecyclerView.Adapter<BackupRecyclerAdapter.MyViewHolder> {

    private Context context;
    private List<BackupModel> data;
    private LayoutInflater inflater;
    private View.OnClickListener onDeleteCLickListener;

    private OnItemClick onItemClick;

    public BackupRecyclerAdapter(Context context, List<BackupModel> data){
        this.context = context;
        this.data = data;
        inflater = LayoutInflater.from(context);

        onItemClick = new OnItemClick() {
            @Override
            public void onItemClickListenter(int pos) {

            }
        };
    }

    public void updateList(List<BackupModel> data){
        this.data = data;
        notifyDataSetChanged();
    }

    public void setOnDeleteCLickListener(View.OnClickListener onDeleteCLickListener){
        this.onDeleteCLickListener = onDeleteCLickListener;
    }

    public void setOnItemClick(OnItemClick onItemClick){
        this.onItemClick = onItemClick;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.backup_list_item_layout, parent, false);
        MyViewHolder holder = new MyViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final int pos = position;
        holder.nameTv.setText(data.get(pos).name);
        holder.dateTv.setText(data.get(pos).dateModified);
        holder.deleteIv.setOnClickListener(onDeleteCLickListener);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //onItemClickListener(pos);
                onItemClick.onItemClickListenter(pos);
            }
        });

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        TextView nameTv;
        TextView dateTv;
        ImageView deleteIv;

        public MyViewHolder(View itemView) {
            super(itemView);

            nameTv = (TextView) itemView.findViewById(R.id.backupTitle);
            dateTv = (TextView) itemView.findViewById(R.id.backupDate);
            deleteIv = (ImageView) itemView.findViewById(R.id.backupDelete);
        }
    }

    public interface OnItemClick{
        public void onItemClickListenter(int pos);
    }
}

package com.seunghoshin.android.musiclist_2;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by SeungHoShin on 2017. 6. 20..
 */

public class Adapter extends RecyclerView.Adapter<Adapter.Holder>{

    Context context;
    List<Music> datas;

    // 아답터 생성자
    public Adapter(Context context) {
        this.context = context;
    }

    // 음악 목록 데이터를 세팅하는 함수
    public void setData(List<Music> datas){
        this.datas = datas;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_main_list,parent,false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        Music music = datas.get(position); // 화면에 나타나는 셀하나당 한번 호출
        holder.textTitle.setText(music.title);
        holder.textArtist.setText(music.artist);


        Glide.with(context)
                .load(music.albumArt)
                .into(holder.imageView);

    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    class Holder extends RecyclerView.ViewHolder{

        ImageView imageView;
        TextView textTitle, textArtist;

        public Holder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            textTitle = (TextView) itemView.findViewById(R.id.textTitle);
            textArtist = (TextView) itemView.findViewById(R.id.textArtist);
        }
    }

}

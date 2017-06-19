package com.seunghoshin.android.musiclist_2;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.List;

public class MainActivity extends AppCompatActivity implements PermissionControl.CallBack {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PermissionControl.checkPermission(this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionControl.onResult(this,requestCode,grantResults);
    }

    @Override
    public void init() {
        // Recyclerview 선언하고
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler);
        // 아답터를 생성(생성자를 받아온다 이때 context를 받게끔함)
        Adapter adapter = new Adapter(this);
        // 데이터 가져오기 (데이타베이스에서 read라는 함수가 데이터를 읽어오는 함수이다)
        List<Music> datas = Database.read(this);
        // 아답터에 데이터 넣기 (setData는 음악 목록 데이터를 세팅하는 함수)
        adapter.setData(datas);
        // 연결
        recyclerView.setAdapter(adapter);
        // 레이아웃 매니저
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}

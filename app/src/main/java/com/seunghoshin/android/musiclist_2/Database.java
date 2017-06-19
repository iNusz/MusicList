package com.seunghoshin.android.musiclist_2;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by SeungHoShin on 2017. 6. 19..
 */

// 음악 목록이 저장되어 있는 저장소를 컨트롤 하는 객체

public class Database {

    // C R U D

    // 데이터를 수정하는 함수
    // 음악제목, 앨범 이름 등을 변경

    public void update() {

    }

    // 음원 데이터를 삭제하는 함수
    public void delete() {


    }

    // 음원 데이터 읽어오는 함수
    public static List<Music> read(Context context) {

        // 0. 먼저 Album Art가 있는 테이블에서 전체 이미지를 조회해서 저장해 둔다
        setAlbumArt(context);
        // (음악id,앨범아트 이미지경로) = hashmap
        // (    1,/sdcar/external/0/medai..../image.jpg)

        // 1. 읽어올 데이터가 뭔지 알아야 됨
        // 읽어올 데이터 = 음악파일
        // 2. 음악파일을 읽어오기 위한 도구 선정 -> 일반적인 프로그래밍에서 File I/O
        //                              -> 안드로이드에서는 Content Resolver

        // 가. 읽어올 데이터의 주소를 설정
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        // 나. 읽어올 데이터의 구체적인 속성(Column)을 정의
        String projection[] = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ARTIST

        };

        // 다. 위에 정의된 주소와 설정값으로 목록을 가져오는 Query(질의) 를 한다
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(musicUri, projection, null, null, null);

        // cursor 는 유지하는 비용이 크다
        // Database 커넥션도 동일하게 연결 유지에 사용되는 비용이 아주 크다
        // 그래서 데이터베이스에서 제공되는 연결객체는 사용 후 즉시 반환하는 것이
        // 성능향상에 도움이 된다.

        // 라. 반복문을 돌면서 cursor 에 있는 데이터를 다른 저장소에 저장한다
        ArrayList<Music> datas = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Music music = new Music();
                music.id = getValue(cursor, projection[0]); // <- 커서에서 id를 꺼내서 담는다
                music.title = getValue(cursor, projection[1]);
                music.albumId = getValue(cursor, projection[2]);
                music.artist = getValue(cursor, projection[3]);

                // 음악 uri
                music.musicUri = makeMusicUri(music.id);

                // 앨범아트 가져오기
                music.albumArt = albumMap.get(Integer.parseInt(music.albumId));

                datas.add(music);
            }
        }

        cursor.close();

        return datas;
    }

    private static String getValue(Cursor cursor, String name) {
        int index = cursor.getColumnIndex(name);
        return cursor.getString(index);
    }

    private static Uri makeMusicUri(String musicId) {
        Uri contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        return Uri.withAppendedPath(contentUri, musicId);
    }

    // 앨범아트 데이터만 따로 저장
    private static HashMap<Integer, String> albumMap = new HashMap<>();

    private static void setAlbumArt(Context context) {

        String[] Album_cursorColumns = new String[]{
                MediaStore.Audio.Albums.ALBUM_ART, //앨범아트
                MediaStore.Audio.Albums._ID
        };

        Cursor Album_cursor = context.getContentResolver().query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                Album_cursorColumns, null, null, null);
        if (Album_cursor != null) { //커서가 널값이 아니면
            if (Album_cursor.moveToFirst()) { //처음참조
                int albumArt = Album_cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART);
                int albumId = Album_cursor.getColumnIndex(MediaStore.Audio.Albums._ID);
                do {
                    if (!albumMap.containsKey(Integer.parseInt(Album_cursor.getString(albumId)))) { //맵에 앨범아이디가 없으면
                        albumMap.put(Integer.parseInt(Album_cursor.getString(albumId)), Album_cursor.getString(albumArt)); //집어넣는다
                    }
                } while (Album_cursor.moveToNext());
            }
        }
        Album_cursor.close();
    }

}

class Music {
    String id;
    String title;
    String albumId;
    String artist;

    Uri musicUri;
    String albumArt;
}

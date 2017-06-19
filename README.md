# MusicList


Database에서 데이터를 읽어오고 화면에 나타나는 과정을 알아보자.



<br/>



## Permission


Mainfest에 읽기 권한을 추가시킨다.


```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
```


그 후에 PermissionControl이라는 클래스를 생성해 권한처리를 몰아두고 **Callback만 Main** 에서 하게끔 만들어 준다.


MainActivity에 PermissionControl.Callback 인터페이스를 implement 시킨후 Main에서 셋팅 해준 것들을


Permission 후에 일어날 수 있도록 Callback 안에 넣어준다


```java
@Override
public void init() {
  RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler);

  ---skip---

  recyclerView.setLayoutManager(new LinearLayoutManager(this));
}
```




그 후에 Main에 다음과 같이 권한을 체크하게끔 onCreate에 넣어주고 Permission 체크 되고 호출되는



onRequestPermissionsResult 를 Override하고 onResult 함수를 실행시키면  자동으로 권한처리를 해준다.



```java
@Override
protected void onCreate(Bundle savedInstanceState) {
  PermissionControl.checkPermission(this);
}
```


```java
@Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
  super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  PermissionControl.onResult(this,requestCode,grantResults);
}
```


<br/>



## Database


음원 데이터를 읽어오는 함수를 만들어 준다.


Cursor는 반복문을 돌면서 cursor에 있는 데이터를 다른 저장소에 저장하는데, 그 이유는 cursor는 유지하는 비용이 크다.


Database 커넥션도 동일하게 연결 유지에 사용되는 비용이 아주 크다


따라서 데이터베이스에서 제공되는 연결객체는 사용 후 즉시 반환하는 것이 성능 향상에 도움이 된다.


<br/>


#### read



```java
public static List<Music> read(Context context) {
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
      music.albumArt = albumMap.get(Integer.parseInt(music.id));

      datas.add(music);
    }
  }
  cursor.close();

  return datas;
}
```



#### getValue


코드의 양을 줄이기 위해 사용 되었다.



```java
private static String getValue(Cursor cursor, String name) {
  int index = cursor.getColumnIndex(name);
  return cursor.getString(index);
}
```




#### makeMusicUri



Music Uri를 만들어 준다.


```java
private static Uri makeMusicUri(String musicId) {
  Uri contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
  return Uri.withAppendedPath(contentUri, musicId);
}
```



<br/>



## Adapter



Adapter와 ViewHolder을 만들어보자.



#### Adapter




우선 기본적으로 생성자와 데이터를 셋팅해준다.




```java
// 아답터 생성자
public Adapter(Context context) {
  this.context = context;
}

// 음악 목록 데이터를 세팅하는 함수
public void setData(List<Music> datas){
  this.datas = datas;
}
```



Adapter에 필요한 함수를 호출 해 준다.


```java
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
```



#### Holder


그 후에 ViewHolder를 상속 받아 Holder를 만들어 준다.


```java
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
```



#### MainActivity



메인에 가서 아답터를 연결해준다.



```java
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
```

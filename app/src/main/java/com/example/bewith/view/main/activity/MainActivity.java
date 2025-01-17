package com.example.bewith.view.main.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.bewith.R;
import com.example.bewith.util.location.LocationProviderManager;
import com.example.bewith.view.main.adapter.CustomAdapter;
import com.example.bewith.view.modify_pop_up.ModifyPopUpActivity;
import com.example.bewith.databinding.ActivityMainBinding;
import com.example.bewith.data.Constants;
import com.example.bewith.view.main.data.CommentData;
import com.example.bewith.view.main.adapter.MyAdapter;
import com.example.bewith.view.main.util.map_item.MarkerCreator;
import com.example.bewith.view.main.util.swipe_menu_list.SwipeMenuListCreator;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;


import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private ActivityMainBinding binding;
    private MainActivityViewModel mainActivityViewModel;
    private GoogleMap mMap;
    public static double myLatitude;
    public static double myLongitude;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private TextView noDataTextView;
    private SwipeMenuListView myCommentListView;
    private RecyclerView commentListView;
    private MyAdapter swipeMenuListAdapter;
    private CustomAdapter listAdapter;

    private ArrayList<CommentData> spinnerArrayList = new ArrayList<>();
    public int radiusIndex = 0;

    private LocationProviderManager locationProviderManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //유니티로 부터 받는 정보
        Intent intent = getIntent();
        Constants.UUID = intent.getStringExtra("UUID");
        myLatitude = Double.parseDouble(intent.getStringExtra("Lat"));
        myLongitude = Double.parseDouble(intent.getStringExtra("Lng"));
        //데이터 바인딩
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        //뷰모델 객체 생성
        mainActivityViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
        //뷰모델 적용
        binding.setViewModel(mainActivityViewModel);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        noDataTextView = binding.noDataTextView;
        myCommentListView = binding.myCommnentListView;
        commentListView = binding.commentListView;
        //땡길 수 있는 리스트뷰 설정
        myCommentListView.setMenuCreator(new SwipeMenuListCreator(getResources()).getCreator(getBaseContext()));

        swipeMenuListAdapter = new MyAdapter(MainActivity.this, spinnerArrayList);//어뎁터에 어레이리스트를 붙임
        myCommentListView.setAdapter(swipeMenuListAdapter);//땡길 수 있는 리스트를 어뎁터에 붙임

        listAdapter = new CustomAdapter(spinnerArrayList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager( this);
        commentListView.setLayoutManager(linearLayoutManager);  // LayoutManager 설정
        commentListView.setAdapter(listAdapter); // 어댑터 설정

        commentListView.setVisibility(View.GONE);

        initListClick();
        initFloatButtonCLick();//플로팅버튼 생성(go to ar)
        createSpinner();//스피너 생성
        initObserver();
        initActivityResult();

        locationProviderManager = new LocationProviderManager(MainActivity.this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mainActivityViewModel.getComment(radiusIndex);
    }


    @Override
    public void onMapReady(final GoogleMap googleMap) {//지도가 준비되면 실행됨
        mainActivityViewModel.getComment(radiusIndex);
        mMap = googleMap;//구글맵을 전역변수 저장
        mainActivityViewModel.getCommentArrayListLiveData().observeInOnStart(this, new Observer<ArrayList<CommentData>>() {
            @Override
            public void onChanged(ArrayList<CommentData> CommentDataList) {
                mMap.clear();
                for (CommentData commentData : CommentDataList) {
                    //마커 생성
                    new MarkerCreator().addMarker(mMap, commentData);
                }
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        LatLng myLocation = new LatLng(myLatitude, myLongitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 18));

    }


    public void createSpinner() {

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.radius_array));
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.radiusSpinner.setAdapter(arrayAdapter);
        binding.radiusSpinner.setSelection(radiusIndex);//초기값

        initSpinnerClick();
    }

    public void initSpinnerClick() {
        binding.radiusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {//스피너가 선택되었을 때
                radiusIndex = position;
                mainActivityViewModel.onSeleteSpinner(radiusIndex);
                if (radiusIndex == 0) {//반경 리스트가 My Comment면
                    myCommentListView.setVisibility(View.VISIBLE);//땡길 수 있는 리스트를 보이게
                    commentListView.setVisibility(View.GONE);//일반 리스트를 안보이게
                } else {//다른게 선택되면
                    myCommentListView.setVisibility(View.GONE);//땡길 수 있는 리스트를 안보이게
                    commentListView.setVisibility(View.VISIBLE);//일반리스트를 보이게
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {//무시하면됨(아무 것도 선택 안됐을 때)
            }
        });
    }

    public void initFloatButtonCLick() {//플로팅버튼 클릭 메소드
        binding.reloadFbtn.setOnClickListener(new View.OnClickListener() {//새로고침 버튼
            @Override
            public void onClick(View v) {
                locationProviderManager.getMyLocation();
                mainActivityViewModel.getComment(radiusIndex);
            }
        });
    }

    public void initListClick() {
        //전체 사용자 comment 리스트 클릭 이벤트
        listAdapter.setOnItemClickListener(new CustomAdapter.OnItemClickListener() {
            //동작 구현
            @Override
            public void onItemClick(View v, int pos) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(spinnerArrayList.get(pos).latitude),
                        Double.parseDouble(spinnerArrayList.get(pos).longitude)), mMap.getCameraPosition().zoom));

            }
        });
        //swipeMenuListView 리스트 열었다 닫았다 메소드
        myCommentListView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {
            @Override
            public void onSwipeStart(int position) {
                // swipe start
                myCommentListView.smoothOpenMenu(position);
            }

            @Override
            public void onSwipeEnd(int position) {
                // swipe end
                myCommentListView.smoothOpenMenu(position);
            }
        });
        //나의 comment 클릭 이벤트
        myCommentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(spinnerArrayList.get(i).latitude),
                        Double.parseDouble(spinnerArrayList.get(i).longitude)), mMap.getCameraPosition().zoom));
            }
        });
        //열려있을때 메뉴 클릭 메소드
        myCommentListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0://수정
                        //코멘트 수정후 돌아왔을 때 실행
                        Intent intent = new Intent(MainActivity.this, ModifyPopUpActivity.class);
                        intent.putExtra("id", spinnerArrayList.get(position)._id);
                        intent.putExtra("category", spinnerArrayList.get(position).category);
                        intent.putExtra("text", spinnerArrayList.get(position).text);
                        activityResultLauncher.launch(intent);
                        break;
                    case 1://삭제
                        mainActivityViewModel.deleteComment(radiusIndex, Integer.toString(spinnerArrayList.get(position)._id));

                        break;
                }
                return true;
            }
        });
    }

    public void initObserver() {
        //스피너 목록에 따라 보여지는 리스트가 변경되면
        mainActivityViewModel.getSpinnerCommentArrayListLiveData().observeInOnStart(this, new Observer<ArrayList<CommentData>>() {
            @Override
            public void onChanged(ArrayList<CommentData> CommentDataList) {
                spinnerArrayList.clear();//비우고 다시 채우기
                if (CommentDataList.isEmpty()) {
                    noDataTextView.setVisibility(View.VISIBLE);//데이터 없음 표시
                } else {
                    noDataTextView.setVisibility(View.INVISIBLE);
                    for (CommentData commentData : CommentDataList) {
                        spinnerArrayList.add(commentData);
                    }//why? notifyDataSetChanged() 얘는 spinnerArrayList= CommentDataList 이런식으로 하면 갱신이 안되더라
                }
                if (radiusIndex == 0) {
                    swipeMenuListAdapter.notifyDataSetChanged();
                } else {
                    listAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    public void initActivityResult() {
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                String _id = result.getData().getStringExtra("id");
                String category = result.getData().getStringExtra("category");
                String text = result.getData().getStringExtra("text");
                mainActivityViewModel.modifyComment(radiusIndex, _id, category, text);
            }
        });
    }
}

// ProgressDialog progressDialog;
// progressDialog = ProgressDialog.show(MainActivity.this,"Please Wait", null, true, true);
//progressDialog.dismiss();



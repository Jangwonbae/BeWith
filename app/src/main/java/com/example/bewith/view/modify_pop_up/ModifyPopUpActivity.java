package com.example.bewith.view.modify_pop_up;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.example.bewith.R;
import com.example.bewith.databinding.ActivityModifyPopUpBinding;
import com.example.bewith.util.network.ModifyComment;
import com.example.bewith.view.main.data.Constants;
import java.util.ArrayList;

public class ModifyPopUpActivity extends Activity {
    private ActivityModifyPopUpBinding binding;

    private int _id;
    private String contents;
    private int categoryIndex;
    private String categoryText;
    private static String IP_ADDRESS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //뷰 바인딩
        binding = ActivityModifyPopUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //서버 IP
        IP_ADDRESS= Constants.IP_ADDRESS;
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //인텐트 받기
        Intent data = getIntent();
        _id = data.getIntExtra("id",-1);
        contents = data.getStringExtra("text");
        categoryIndex = data.getIntExtra("category",-1);
        binding.contentsEditText.setText(contents);

        createCategorySpinner();//카테고리 스피너 생성
        initButtonClick();//버튼 클릭 리스너
    }
    public void createCategorySpinner(){//카테고리 스피너 생성
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.category_array));
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.categorySpinner.setAdapter(arrayAdapter);
        binding.categorySpinner.setSelection(categoryIndex);//초기값
        binding.categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        categoryText = "리뷰";
                        break;
                    case 1:
                        categoryText = "꿀팁";
                        break;
                    case 2:
                        categoryText = "기록";
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }
    public void initButtonClick(){
        binding.okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.contentsEditText.getText().toString().trim();//텍스트 내용
                if(contents.equals("")){
                    Toast.makeText(ModifyPopUpActivity.this,"내용을 입력하세요.",Toast.LENGTH_SHORT).show();
                }
                else{
                    //동기 처리 되어야함
                    ModifyComment modifyComment = new ModifyComment();//내 comment 내용 서버에 전송
                    modifyComment.execute("http://" + IP_ADDRESS + "/updateComment.php",Integer.toString(_id),categoryText,contents);//서버에 전송
                    //동기 처리
                    //갱신메소드
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });
        binding.noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }
}
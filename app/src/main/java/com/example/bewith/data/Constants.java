package com.example.bewith.data;

import com.example.bewith.view.community.data.LikeData;
import com.example.bewith.view.community.data.ReplyData;
import com.example.bewith.view.main.data.CommentData;

import java.util.ArrayList;

public class Constants {
    public static String UUID;
    public static ArrayList<CommentData> commnentDataArrayList = new ArrayList<>();//comment 정보
    public static ArrayList<ReplyData> replyDataArrayList = new ArrayList<>();//댓글 정보
    public static LikeData likeData = new LikeData();
    public static final String IP_ADDRESS = "221.147.144.65:80";

}
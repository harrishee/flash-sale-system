package com.hanfei.flashsales.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanfei.flashsales.pojo.User;
import com.hanfei.flashsales.vo.Result;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: harris
 * @time: 2023
 * @summary: flash-sales
 */
public class UserUtils {

    public static void createUser(int count) throws Exception {
        List<User> users = new ArrayList<>(count);
        // 生成用户
        for (int i = 0; i < count; i++) {
            User user = new User();
            user.setUserId(Long.valueOf(String.valueOf(13000000000L + i)));
            user.setUsername("user " + i);
            user.setSalt("1a2b3c4d");
            user.setPassword(MD5Utils.inputPassToDBPass("121212", user.getSalt()));
            user.setAddress("广州市");
            users.add(user);
        }
        System.out.println("生成用户完毕");

        Connection conn = getConn();
        String sql = "insert into fs_user(user_id, username, password, salt, address)values(?,?,?,?,?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            pstmt.setLong(1, user.getUserId());
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, user.getPassword());
            pstmt.setString(4, user.getSalt());
            pstmt.setString(5, user.getAddress());
            pstmt.addBatch();
        }
        pstmt.executeBatch();
        pstmt.close();
        conn.close();
        System.out.println("已插入到数据库");


        // 登录，生成 ticket
        String urlString = "http://localhost:8080/login/processLogin";
        File file = new File("/Users/harris/config.txt");
        if (file.exists()) {
            file.delete();
        }
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        file.createNewFile();
        raf.seek(0);
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            URL url = new URL(urlString);
            HttpURLConnection co = (HttpURLConnection) url.openConnection();
            co.setRequestMethod("POST");
            co.setDoOutput(true);
            OutputStream out = co.getOutputStream();
            String params = "userId=" + user.getUserId() + "&password=" + MD5Utils.inputPassToFormPass("121212");
            out.write(params.getBytes());
            out.flush();
            InputStream inputStream = co.getInputStream();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte buff[] = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buff)) >= 0) {
                bout.write(buff, 0, len);
            }
            inputStream.close();
            bout.close();
            String response = new String(bout.toByteArray());
            ObjectMapper mapper = new ObjectMapper();
            Result respBean = mapper.readValue(response, Result.class);
            String userTicket = ((String) respBean.getObject());
            String row = user.getUserId() + "," + userTicket;
            raf.seek(raf.length());
            raf.write(row.getBytes());
            raf.write("\r\n".getBytes());
        }
        raf.close();
        System.out.println("写入 ticket 完毕");
    }

    private static Connection getConn() throws Exception {
        String url = "jdbc:mysql://localhost:3306/flash_sales";
        String username = "root";
        String password = "123456";
        String driver = "com.mysql.cj.jdbc.Driver";
        Class.forName(driver);
        return DriverManager.getConnection(url, username, password);
    }
}

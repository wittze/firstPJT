package com.zewei.firstpjt.service;


import com.zewei.firstpjt.dao.UserMapper;
import com.zewei.firstpjt.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

}

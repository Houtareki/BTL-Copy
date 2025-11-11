package com.example.demo.Service;

import org.springframework.stereotype.Service;

import com.example.demo.Model.CommonModel.EmailDetail;

@Service
public interface EmailService {
    public void sendEmail(EmailDetail emailDetail);
}

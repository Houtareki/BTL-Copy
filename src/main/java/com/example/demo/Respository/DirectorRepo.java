package com.example.demo.Respository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.Entity.Director;

@Repository
public interface DirectorRepo extends JpaRepository<Director,Integer> {

}

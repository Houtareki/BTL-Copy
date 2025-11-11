package com.example.demo.Respository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.Entity.Actor;

@Repository
public interface ActorRepo extends JpaRepository<Actor,Integer>{
    

}

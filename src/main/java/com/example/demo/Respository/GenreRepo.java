package com.example.demo.Respository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.Entity.Genre;

import jakarta.transaction.Transactional;

@Repository
public interface GenreRepo extends JpaRepository<Genre, Integer>{
    
    @Transactional
    @Query(value = "select * from genres", nativeQuery = true)
    public List<Genre> getAllGenres();
}

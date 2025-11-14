package com.example.demo.Respository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.Entity.Episode;

import java.util.List;
import java.util.Optional;


@Repository
public interface EpisodeRepo extends JpaRepository<Episode,Integer>{
    public Optional<Episode> findByEpisodeId(int episodeId);
    public List<Episode> findAllEpisodeByMovieId(int movieId);

    @Modifying
    @Transactional
    @Query("UPDATE Episode e SET e.movieId = NULL WHERE e.movieId = :movieId")
    void unlinkEpisodesFromMovie(@Param("movieId") int movieId);
}

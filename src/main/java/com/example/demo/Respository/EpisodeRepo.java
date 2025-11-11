package com.example.demo.Respository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.Entity.Episode;

import java.util.List;
import java.util.Optional;


@Repository
public interface EpisodeRepo extends JpaRepository<Episode,Integer>{
    public Optional<Episode> findByEpisodeId(int episodeId);
    public List<Episode> findAllEpisodeByMovieId(int movieId);
}

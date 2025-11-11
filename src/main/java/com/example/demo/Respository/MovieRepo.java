package com.example.demo.Respository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.Entity.Movie;

import jakarta.transaction.Transactional;

@Repository
public interface MovieRepo extends JpaRepository<Movie,Integer>{
    
    public Movie findByMovieId(int movieId);

    @Transactional
    @Query(value = "select distinct * from movies order by release_date desc limit 10", nativeQuery = true)
    public List<Movie> getBannerMovies();

    @Transactional
    @Query(value = "select distinct * from movies where status='showing' limit 10", nativeQuery = true)
    public List<Movie> getShowingMovies();

    @Transactional
    @Query(value = "select views from movies where movie_id = :movieId", nativeQuery = true)
    public int findAllViewsByMovieId(@Param(value = "movieId") int movieId);

    @Transactional
    @Query(value = "select * from movies order by views limit 10", nativeQuery = true)
    public List<Movie> getSuggestedMovies();

    @Transactional
    @Query(value = "select distinct country from movies", nativeQuery = true)
    public List<String> getAllCountries();

    @Transactional
    @Query(value = "select m.* from movies m left join movie_genres mg on m.movie_id = mg.movie_id where mg.genre_id = :genreId", nativeQuery = true)
    public List<Movie> getAllMoviesByGenre(@Param(value = "genreId") int genreId);

    @Transactional
    @Query(value = "select * from movies where country = :country", nativeQuery = true)
    public List<Movie> getAllMoviesByCountry(@Param(value = "country") String country);

    @Transactional
    @Modifying
    @Query(value = "update movies set views = views+1 where movie_id = :movieId", nativeQuery = true)
    public void updateMovieViews(@Param(value = "movieId") int movieId);
}



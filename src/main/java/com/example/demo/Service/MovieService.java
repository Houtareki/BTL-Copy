package com.example.demo.Service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.Entity.Comment;
import com.example.demo.Entity.Episode;
import com.example.demo.Entity.Genre;
import com.example.demo.Entity.Movie;
import com.example.demo.Model.CommonModel.CommentForm;
import com.example.demo.Model.ResponseModel.CommentData;
import com.example.demo.Model.ResponseModel.MovieInteractionData;

@Service
public interface MovieService {
    public List<Movie> getBannerMovies();
    public Movie getMovieByMovieId(int movieId);
    public List<Episode> findAllEpisodeByMovieId(int movieId);
    public Movie findMovieById(int movieId);
    public List<Movie> getShowingMovies(int userId);
    public MovieInteractionData getMovieInteractionData(int userId, int movieId);
    public List<Movie> getSuggestedMovies(int userId);
    public List<Genre> getAllGenres(int userId);
    public List<String> getAllCountries(int userId);
    public List<CommentData> getAllCommentsByMovieId(int userId, int movieId);
    public Comment addComment(CommentForm commentForm);
    public Movie updateMovieViews(int userId, int movieId);
    public List<Movie> getAllMoviesByGenreId(int userId, int genreId);
    public List<Movie> getAllMoviesByCountry(int userId, String country);
    public List<Movie> getLikedMoviesByUserId(int userId);
    public List<Movie> getSavedMoviesByUserId(int userId);
    public List<Movie> getWatchedMoviesByUserId(int userId);
}

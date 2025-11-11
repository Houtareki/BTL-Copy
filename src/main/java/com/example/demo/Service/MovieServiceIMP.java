package com.example.demo.Service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.Entity.Comment;
import com.example.demo.Entity.Episode;
import com.example.demo.Entity.Genre;
import com.example.demo.Entity.Movie;
import com.example.demo.Entity.User;
import com.example.demo.Model.CommonModel.CommentForm;
import com.example.demo.Model.ResponseModel.CommentData;
import com.example.demo.Model.ResponseModel.MovieInteractionData;
import com.example.demo.Respository.CommentRepo;
import com.example.demo.Respository.EpisodeRepo;
import com.example.demo.Respository.FavoriteRepo;
import com.example.demo.Respository.GenreRepo;
import com.example.demo.Respository.MovieRepo;
import com.example.demo.Respository.SaveRepo;
import com.example.demo.Respository.UserRepo;
import com.example.demo.Respository.WatchHistoryRepo;

@Service
public class MovieServiceIMP implements MovieService {

    @Autowired
    private MovieRepo movieRepo;

    @Autowired
    private EpisodeRepo episMovieRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private FavoriteRepo favoriteRepo;

    @Autowired
    private SaveRepo saveRepo;

    @Autowired
    private WatchHistoryRepo watchHistoryRepo;

    @Autowired
    private GenreRepo genreRepo;

    @Autowired
    private CommentRepo commentRepo;

    public List<Movie> getBannerMovies() {
        List<Movie> bannerMovies = movieRepo.getBannerMovies();
        return bannerMovies;
    }

    public Movie getMovieByMovieId(int movieId) {
        Movie movie = movieRepo.findById(movieId).get();
        return movie;
    }

    public List<Episode> findAllEpisodeByMovieId(int movieId) {
        List<Episode> episodes = episMovieRepo.findAllEpisodeByMovieId(movieId);
        return episodes;
    }

    public Movie findMovieById(int movieId) {
        Movie movie = movieRepo.findById(movieId).get();
        return movie;
    }

    public MovieInteractionData getMovieInteractionData(int userId, int movieId) {
        MovieInteractionData res = new MovieInteractionData(userId, movieId, 0, 0, 0, false, false);
        User user = this.userRepo.findByUserId(userId);
        Movie movie = this.movieRepo.findByMovieId(movieId);
        if(user==null || movie == null) {
            return null;
        } 

        res.setTotalLikes(favoriteRepo.findTotalLikesByMovieId(movieId));
        res.setTotalSaves(saveRepo.findTotalSavesByMovieId(movieId));
        res.setTotalView(movieRepo.findAllViewsByMovieId(movieId));
        res.setUserLikedThis(favoriteRepo.isMovieFavoritedByUser(userId, movieId)==0 ? false : true);
        res.setUserSavedThis(saveRepo.isMovieSavedByUser(userId, movieId)==0 ? false : true);
        return res;
    }

    public List<Movie> getShowingMovies(int userId) {
        User user = this.userRepo.findByUserId(userId);
        if(user!=null) {
            List<Movie> showingMovies = this.movieRepo.getShowingMovies();
            return showingMovies;
        }
        return null;
    }

    public List<Movie> getSuggestedMovies(int userId) {
        User user = this.userRepo.findByUserId(userId);
        if(user!=null) {
            List<Movie> suggestedMobies = this.movieRepo.getSuggestedMovies();
            return suggestedMobies;
        }
        return null;
    }

    public List<Genre> getAllGenres(int userId) {
        User user = this.userRepo.findByUserId(userId);
        if(user!=null) {
            List<Genre> genres = this.genreRepo.getAllGenres();
            return genres;
        }
        return null;
    }

    public List<String> getAllCountries(int userId) {
        User user = this.userRepo.findByUserId(userId);
        if(user!=null) {
            List<String> genres = this.movieRepo.getAllCountries();
            return genres;
        }
        return null;
    }

    public List<CommentData> getAllCommentsByMovieId(int userId, int movieId) {
        User user = this.userRepo.findByUserId(userId);
        if(user!=null) {
            List<Comment> comments = this.commentRepo.getAllCommentsByMovieId(movieId);
            List<CommentData> commentDatas = new ArrayList<>();
            for(Comment comment : comments) {
                commentDatas.add(new CommentData(comment.getCommentId(), comment.getUserId(), comment.getMovieId(), comment.getContent(), comment.getCreatedAt(), this.userRepo.findByUserId(comment.getUserId()).getUsername()));
            }
            return commentDatas;
        }
        return null;
    }

    public Comment addComment(CommentForm commentForm) {
        int userId = commentForm.getUserId();
        int movieId = commentForm.getMovieId();
        String content = commentForm.getContent();
        User user = userRepo.findByUserId(userId);
        Movie movie = movieRepo.findByMovieId(movieId);
        if(user!=null && movie!=null && content!=null && content.length()>0) {
            commentRepo.addComment(userId, movieId, content);
            return new Comment();
        }
        return null;
    }

    @Override
    public Movie updateMovieViews(int userId, int movieId) {
        User user = userRepo.findByUserId(userId);
        Movie movie = movieRepo.findByMovieId(movieId);
        if(user!=null && movie!=null) {
            // Cập nhật lượt xem
            this.movieRepo.updateMovieViews(movieId);

            // Thêm vào lịch sử xem (nếu chưa có)
            int alreadyWatched = this.watchHistoryRepo.checkIfWatched(userId, movieId);
            if(alreadyWatched == 0) {
                this.watchHistoryRepo.addToWatchHistory(userId, movieId);
            }

            return movie;
        }
        return null;
    }

    public List<Movie> getAllMoviesByGenreId(int userId, int genreId) {
        User user = userRepo.findByUserId(userId);
        if(user!=null) {
            List<Movie> movies = movieRepo.getAllMoviesByGenre(genreId);
            if(movies!=null && movies.size()>0) {
                return movies;
            }
            return null;
        }
        return null;
    }

    public List<Movie> getAllMoviesByCountry(int userId, String country) {
        User user = userRepo.findByUserId(userId);
        if(user!=null) {
            List<Movie> movies = movieRepo.getAllMoviesByCountry(country);
            if(movies!=null && movies.size()>0) {
                return movies;
            }
            return null;
        }
        return null;
    }

    @Override
    public List<Movie> getLikedMoviesByUserId(int userId) {
        User user = this.userRepo.findByUserId(userId);
        if(user!=null) {
            List<Movie> movies = this.favoriteRepo.findAllFavoriedMovieByUerId(userId);
            return movies;
        }
        return null;
    }

    @Override
    public List<Movie> getSavedMoviesByUserId(int userId) {
        User user = this.userRepo.findByUserId(userId);
        if(user!=null) {
            List<Movie> movies = this.saveRepo.findAllSavedMovieByUserId(userId);
            return movies;
        }
        return null;
    }

    @Override
    public List<Movie> getWatchedMoviesByUserId(int userId) {
        User user = this.userRepo.findByUserId(userId);
        if(user!=null) {
            List<Movie> movies = this.watchHistoryRepo.findWatchedMovieByUserId(userId);
            return movies;
        }
        return null;
    }
}

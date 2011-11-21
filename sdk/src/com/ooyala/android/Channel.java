package com.ooyala.android;

import java.util.ArrayList;

public class Channel extends ContentItem
{
  protected ArrayList<Movie>_movies = new ArrayList<Movie>();

  protected void addMovie(Movie movie)
  {
    _movies.add(movie);
  }

  public ArrayList<Movie> getMovies()
  {
    return _movies;
  }
}

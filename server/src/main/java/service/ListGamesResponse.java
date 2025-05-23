package service;

import model.GameData;
import java.util.Map;

public record ListGamesResponse(Map<Integer, GameData> games) {}

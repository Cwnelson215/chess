package service;

import model.GameData;
import java.util.Map;

public record ListGamesResponse(Map<String, GameData> games) {}

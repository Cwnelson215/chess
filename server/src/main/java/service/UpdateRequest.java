package service;

import model.GameData;

public record UpdateRequest(GameData game, String gameID) {
}

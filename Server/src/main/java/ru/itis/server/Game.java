package ru.itis.server;

import ru.itis.models.Player;
import ru.itis.models.Question;
import ru.itis.protocol.message.server.GameOverMessage;
import ru.itis.protocol.message.server.NextQuestionMessage;
import ru.itis.repository.QuestionRepositoryImpl;

import java.util.HashMap;
import java.util.List;

public class Game implements Runnable{
    private RoomService room;
    private HashMap<Integer, Player> players;
    private List<Question> questions;

    private QuestionRepositoryImpl repository ;

    private int currentQ = 1;
    private final int TIME_FOR_QUESTION = 15;

    public void start(RoomService room, HashMap<Integer,Player> connections) {
        this.room = room;
        this.players = connections;
        repository = new QuestionRepositoryImpl();
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        try {
            while(currentQ < 10){
                room.sendToConnections(new NextQuestionMessage(repository.getQuestion(), -1));
                Thread.sleep(TIME_FOR_QUESTION);
                // здесь может быть обработка ответов
                currentQ++;
            }
            gameOver();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    private void gameOver(){
        room.sendToConnections(new GameOverMessage((List<Player>) players.values(), room.getRoom().getId()));
        room.finishGame();
    }
    public void playerDisconnected(int id){
        if (players.get(id) == null){
            return;
        }
        players.remove(id);
    }
}

package com.gruppo3.ai.lab3.model;

//classe che formatta la risposta di ritorno all'utente
//quando fa richiesta di un poligono
public class PositionPacket {

    private int positions_number;
    private int users_number;

    public PositionPacket(int position_number, int users_number){
        this.positions_number = position_number;
        this.users_number = users_number;
    }
    public int getPosition_numbers() {
        return positions_number;
    }

    public void setPosition_numbers(int position_number) {
        this.positions_number = position_number;
    }

    public int getUsers_numbers() {
        return users_number;
    }

    public void setUsers_numbers(int users_number) {
        this.users_number = users_number;
    }
}

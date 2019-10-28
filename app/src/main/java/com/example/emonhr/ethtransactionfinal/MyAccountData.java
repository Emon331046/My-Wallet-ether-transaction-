package com.example.emonhr.ethtransactionfinal;



public class MyAccountData {

    int id;
    String password;
    String fileName;

    public MyAccountData(int id, String password, String fileName) {
        this.id = id;
        this.fileName = fileName;
        this.password=password;
    }

    public String getColorCode() {
        return password;
    }

    public void setColorCode(String password) {
        this.password = password;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }



    public String getDescription() {
        return fileName;
    }

    public void setDescription(String description) {
        this.fileName = description;
    }


}

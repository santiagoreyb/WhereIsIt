package com.example.whereisit;

import java.util.ArrayList;
import java.util.List;

public class PersonaClass {

    private String uid;
    private String nombre;
    private String email;
    private String tipo;

    private boolean disponible;
    private boolean toastMostrado;
    public List<String> activos;


    public String getUid() {
        return uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTipo() {
        return this.tipo;
    }
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
    public boolean getDisponible() {
        return disponible;
    }

    public boolean getToastMostrado() {
        return toastMostrado;
    }
    public void setToastMostrado(boolean toastMostrado) {
        this.toastMostrado = toastMostrado;
    }

    public List<String> getActivos() {
        return activos;
    }

    public void setActivos(List<String> activos) {
        this.activos = activos;
    }

}


package com.kuycoding.wijayafarm.model;


import java.util.Date;

public class Panen {
    private String id;
    private String uid;
    private String id_ayam;
    private String harga_kg;
    private String harga_total;
    private String jumlah_ayam;
    private String nama_pembeli;
    private String periode;
    private String tanggal;
    private String total_berat;

    public Panen() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getId_ayam() {
        return id_ayam;
    }

    public void setId_ayam(String id_ayam) {
        this.id_ayam = id_ayam;
    }

    public String getHarga_kg() {
        return harga_kg;
    }

    public void setHarga_kg(String harga_kg) {
        this.harga_kg = harga_kg;
    }

    public String getHarga_total() {
        return harga_total;
    }

    public void setHarga_total(String harga_total) {
        this.harga_total = harga_total;
    }

    public String getJumlah_ayam() {
        return jumlah_ayam;
    }

    public void setJumlah_ayam(String jumlah_ayam) {
        this.jumlah_ayam = jumlah_ayam;
    }

    public String getNama_pembeli() {
        return nama_pembeli;
    }

    public void setNama_pembeli(String nama_pembeli) {
        this.nama_pembeli = nama_pembeli;
    }

    public String getPeriode() {
        return periode;
    }

    public void setPeriode(String periode) {
        this.periode = periode;
    }

    public String getTanggal() {
        return tanggal;
    }

    public void setTanggal(String tanggal) {
        this.tanggal = tanggal;
    }

    public String getTotal_berat() {
        return total_berat;
    }

    public void setTotal_berat(String total_berat) {
        this.total_berat = total_berat;
    }
}

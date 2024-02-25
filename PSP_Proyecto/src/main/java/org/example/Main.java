package org.example;

import org.json.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    static String url = "192.168.1.37:5432/adt_proyecto";
    static String usuario = "admin";
    static String contrasenya = "1234";
    public static void main(String[] args) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        executor.scheduleAtFixedRate(() -> {
        ArrayList <Usuario> usuarios = null;
        try {
            usuarios = obtenerUsuarios();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

        ArrayList <Resenya> resenyas = obtenerResenyas();

        calcularDatos(usuarios,resenyas);

        actualizarDatos(usuarios,resenyas);
    }, 0, 1, TimeUnit.HOURS);



    }


    //LEER
    public static ArrayList<Usuario> obtenerUsuarios() throws Exception { //ODOO
        JSONObject requestObject = new JSONObject();
        requestObject.put("jsonrpc", "2.0");
        requestObject.put("method", "call");

        JSONObject paramsObject = new JSONObject();
        paramsObject.put("service", "object");
        paramsObject.put("method", "execute");

        JSONArray argsArray = new JSONArray();
        argsArray.put("Proyecto");
        argsArray.put(2);
        argsArray.put("admin");
        argsArray.put("res.users");
        argsArray.put("search_read");
        argsArray.put(new JSONArray()); // Filtro vacío

        JSONArray fieldsArray = new JSONArray();
        fieldsArray.put("id");
        fieldsArray.put("name");
        fieldsArray.put("karma");
        fieldsArray.put("is_premium");

        argsArray.put(fieldsArray);
        paramsObject.put("args", argsArray);

        requestObject.put("params", paramsObject);
        requestObject.put("id", 1);

        URL url = new URL("http://192.168.1.37:8069/jsonrpc");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);

        String requestBody = requestObject.toString();
        con.getOutputStream().write(requestBody.getBytes());

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder responseBuilder = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            responseBuilder.append(inputLine);
        }
        in.close();

        String responseBody = responseBuilder.toString();

        JSONObject responseObject = new JSONObject(responseBody);
        JSONArray usersArray = responseObject.getJSONArray("result");
        ArrayList <Usuario> usuarios= new ArrayList<>();
        for (int i = 0; i < usersArray.length(); i++) {
            JSONObject userObject = usersArray.getJSONObject(i);
            int id = userObject.getInt("id");
            int karma = userObject.getInt("karma");
            boolean premium = userObject.getBoolean("is_premium");
            usuarios.add(new Usuario(id,karma,premium));

        }
        con.disconnect();
        return usuarios;

    }


    public static ArrayList<Resenya> obtenerResenyas() {
        ArrayList<Resenya> resenyas = new ArrayList<>();
        String jdbcUrl = "jdbc:postgresql://" + url;

        try (Connection conexion = DriverManager.getConnection(jdbcUrl, usuario, contrasenya)) {
            Statement statement = conexion.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM resenya");

            while (resultSet.next()) {
                Integer id = resultSet.getInt("id");
                Integer usuario = resultSet.getInt("usuario_uid");
                Integer valoracion = resultSet.getInt("valoracion");
                resenyas.add(new Resenya(id,usuario, valoracion));
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            System.err.println("Error al conectar a la base de datos PostgreSQL: " + e.getMessage());
        }

        return resenyas;
    }

    public static void modificarUsuario(int id, float nuevoKarma) throws Exception {
        JSONObject requestObject = new JSONObject();
        requestObject.put("jsonrpc", "2.0");
        requestObject.put("method", "call");

        JSONObject paramsObject = new JSONObject();
        paramsObject.put("service", "object");
        paramsObject.put("method", "execute");

        JSONArray argsArray = new JSONArray();
        argsArray.put("Proyecto");
        argsArray.put(2);
        argsArray.put("admin");
        argsArray.put("res.users");
        argsArray.put("write");

        argsArray.put(id);

        JSONObject valsObject = new JSONObject();
        valsObject.put("karma", nuevoKarma);


        argsArray.put(valsObject);

        paramsObject.put("args", argsArray);

        requestObject.put("params", paramsObject);
        requestObject.put("id", 16677686);

        URL url = new URL("http://192.168.1.37:8069/jsonrpc");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);

        String requestBody = requestObject.toString();
        con.getOutputStream().write(requestBody.getBytes());
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder responseBuilder = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            responseBuilder.append(inputLine);
            System.out.println(inputLine);
        }
        in.close();

        con.disconnect();
    }


    public static void actualizarDatos(ArrayList<Usuario> usuarios, ArrayList<Resenya> resenyas){

        for (Usuario usuario : usuarios){
            try {
                modificarUsuario(usuario.getId(),usuario.getKarma());
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
        }

    }

    public static void calcularDatos(ArrayList<Usuario> usuarios, ArrayList<Resenya> resenyas) {

        for (Resenya resenya : resenyas) {

            for (Usuario usuario : usuarios) {

                if (resenya.getUsuario() == usuario.getId()) {

                    if (resenya.getValoracion() >= 3) {
                        usuario.setKarma(usuario.getKarma() + 10);

                    } else {
                        usuario.setKarma(usuario.getKarma() - 5);

                    }

                }
                
            }
        }

    }
}
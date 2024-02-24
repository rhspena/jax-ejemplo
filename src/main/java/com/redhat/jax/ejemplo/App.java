package com.redhat.jax.ejemplo;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import com.google.api.client.util.ExponentialBackOff;

public class App {

    public static void main(String[] args) {

        System.out.println("Prueba sincrona para obtener el Todo 1");
        Todo todo1 = execAsNormal();
        System.out.println(todo1.toString());

        System.out.println("Prueba asincrona para obtener el Todo 2");
        Todo todo2 = execAsAsync();
        System.out.println(todo2.toString());

        System.out.println("Prueba asincrona para obtener todos los Todo's con Backoff");
        Response response = null;
        TodoTask task = new TodoTask(response);
        
        List<Todo> list = execWithBackoff(task).readEntity(new GenericType<List<Todo>>() {
        });

        for (int index = 0; index < 5; index++) {
            System.out.println(list.get(index).toString());
        }

        System.exit(0);
    }

    private static Todo execAsNormal() {
        Client client = ClientBuilder.newClient();

        WebTarget webTarget = client.target("https://jsonplaceholder.typicode.com/todos/1");

        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);

        Todo result = invocationBuilder.get(Todo.class);

        return result;
    }

    private static Todo execAsAsync() {
        Client client = ClientBuilder.newClient();

        WebTarget webTarget = client.target("https://jsonplaceholder.typicode.com/todos/2");

        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);

        Future<Todo> future = invocationBuilder.async().get(Todo.class);

        try {
            return future.get(5000, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            System.out.println("Time out.");
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Response execWithBackoff(Callable<Response> i) {
        ExponentialBackOff backoff = new ExponentialBackOff.Builder().build();

        long delay = 0;

        Response response;
        do {
            try {
                Thread.sleep(delay);

                response = i.call();

                if (response.getStatusInfo().getFamily() == Family.SERVER_ERROR) {
                    System.out.println(String.format("Server error {} when accessing path {}. Delaying {}ms", response.getStatus(),
                            response.getLocation().toASCIIString(), delay));
                }

                delay = backoff.nextBackOffMillis();
            } catch (Exception e) { // callable throws exception
                throw new RuntimeException("Client request failed", e);
            }

        } while (delay != ExponentialBackOff.STOP && response.getStatusInfo().getFamily() == Family.SERVER_ERROR);

        if (response.getStatusInfo().getFamily() == Family.SERVER_ERROR) {
            throw new IllegalStateException("Client request failed for " + response.getLocation().toASCIIString());
        }

        return response;
    }

}

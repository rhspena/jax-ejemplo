package com.redhat.jax.ejemplo;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class TodoTask implements Callable<Response> {

    private Response response;    

    public TodoTask() { }

    public TodoTask(Response response) {
        this.response = response;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    @Override
    public Response call() throws Exception {
        
        Client client = ClientBuilder.newClient();

        WebTarget webTarget = client.target("https://jsonplaceholder.typicode.com/todos");

        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);

        Future<Response> future = invocationBuilder.async().get();

        try {
            return future.get(5000, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            System.out.println("Time out.");
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }
    
}

package com.chemecador.secretaria.api

import com.chemecador.secretaria.items.Friend
import com.chemecador.secretaria.items.Note
import com.chemecador.secretaria.items.NotesList
import com.chemecador.secretaria.items.Task
import com.chemecador.secretaria.requests.LoginRequest
import com.chemecador.secretaria.requests.NoteRequest
import com.chemecador.secretaria.requests.PasswordRequest
import com.chemecador.secretaria.requests.TaskRequest
import com.chemecador.secretaria.responses.IdResponse
import com.chemecador.secretaria.responses.login.LoginResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface Service {
    @POST("/login")
    @Headers("Content-Type: application/json")
    fun login(@Body request: LoginRequest?): Call<LoginResponse?>

    @POST("/register")
    @Headers("Content-Type: application/json")
    fun register(@Body request: LoginRequest?): Call<LoginResponse?>

    /* GETTERS */
    @GET("/users/{userId}/lists")
    @Headers("Content-Type: application/json")
    fun getLists(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int
    ): Call<ArrayList<NotesList>>

    @GET("/users/{userId}/notes")
    @Headers("Content-Type: application/json")
    fun getNotes(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int
    ): Call<ArrayList<Note>>

    @GET("/users/{userId}/tasks")
    @Headers("Content-Type: application/json")
    fun getTasks(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int
    ): Call<ArrayList<Task>>

    @GET("/users/{userId}/friendRequests")
    fun getFriendRequests(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int
    ): Call<ArrayList<Friend>>


    @GET("/users/{userId}/friends")
    fun getFriends(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int
    ): Call<ArrayList<Friend>>

    /* CREATE */
    @POST("/users/{id}/lists")
    @Headers("Content-Type: application/json")
    fun createList(
        @Header("Authorization") token: String,
        @Path("id") userId: Int,
        @Body list: NotesList?
    ): Call<IdResponse>

    @POST("/users/{id}/lists/{listId}/notes")
    @Headers("Content-Type: application/json")
    fun createNote(
        @Header("Authorization") token: String,
        @Path("id") userId: Int,
        @Path("listId") listId: Int,
        @Body nr: NoteRequest?
    ): Call<IdResponse>

    @POST("/users/{id}/tasks")
    @Headers("Content-Type: application/json")
    fun createTask(
        @Header("Authorization") token: String,
        @Path("id") userId: Int,
        @Body tr: TaskRequest?
    ): Call<IdResponse>

    /* UPDATE */
    @PUT("/users/{userId}/lists/{listId}")
    @Headers("Content-Type: application/json")
    fun updateList(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int,
        @Path("listId") listId: Int,
        @Body list: NotesList?
    ): Call<ResponseBody>

    @PUT("/users/{userId}/lists/{listId}/notes/{noteId}")
    @Headers("Content-Type: application/json")
    fun updateNote(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int,
        @Path("listId") listId: Int,
        @Path("noteId") noteId: Int,
        @Body nr: NoteRequest?
    ): Call<ResponseBody>

    @PUT("/users/{userId}/tasks/{taskId}")
    @Headers("Content-Type: application/json")
    fun updateTask(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int,
        @Path("taskId") taskId: Int,
        @Body tr: TaskRequest?
    ): Call<ResponseBody>

    /* DELETE */
    @DELETE("/users/{id}/lists/{listId}/notes/{noteId}")
    @Headers("Content-Type: application/json")
    fun deleteNote(
        @Header("Authorization") token: String,
        @Path("id") userId: Int,
        @Path("listId") listId: Int,
        @Path("noteId") noteId: Int
    ): Call<ResponseBody>

    @DELETE("/users/{id}/lists/{listId}")
    @Headers("Content-Type: application/json")
    fun deleteList(
        @Header("Authorization") token: String,
        @Path("id") userId: Int,
        @Path("listId") listId: Int
    ): Call<ResponseBody>

    @DELETE("/users/{id}/tasks/{taskId}")
    @Headers("Content-Type: application/json")
    fun deleteTask(
        @Header("Authorization") token: String,
        @Path("id") userId: Int,
        @Path("taskId") taskId: Int
    ): Call<ResponseBody>

    /* PASSWORD */
    @POST("/users/{id}/account/changePassword")
    @Headers("Content-Type: application/json")
    fun changePassword(
        @Header("Authorization") token: String,
        @Path("id") userId: Int,
        @Body pr: PasswordRequest?
    ): Call<ResponseBody>
}
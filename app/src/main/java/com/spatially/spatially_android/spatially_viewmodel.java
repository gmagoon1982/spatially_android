package com.spatially.spatially_android;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class spatially_viewmodel extends ViewModel {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private MutableLiveData<String> email = new MutableLiveData<>();
    private MutableLiveData<String> password = new MutableLiveData<>();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private MutableLiveData<ArrayList> fence_ids = new MutableLiveData<>();
    private MutableLiveData<ArrayList> fence_names = new MutableLiveData<>();
    private MutableLiveData<ArrayList> fence_radius = new MutableLiveData<>();
    private MutableLiveData<ArrayList> fence_center_latitude = new MutableLiveData<>();
    private MutableLiveData<ArrayList> fence_center_longitude = new MutableLiveData<>();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private MutableLiveData<ArrayList> name  = new MutableLiveData<>();
    private MutableLiveData<ArrayList> account_creation_time = new MutableLiveData<>();
    private MutableLiveData<ArrayList> id_time  = new MutableLiveData<>();
    private MutableLiveData<ArrayList> fences  = new MutableLiveData<>();
    private MutableLiveData<ArrayList> email_info  = new MutableLiveData<>();
    private MutableLiveData<ArrayList> battery_info  = new MutableLiveData<>();
    private MutableLiveData<ArrayList> accuracy_info  = new MutableLiveData<>();
    private MutableLiveData<ArrayList> latitude = new MutableLiveData<>();
    private MutableLiveData<ArrayList> longitude  = new MutableLiveData<>();
    private MutableLiveData<ArrayList> id  = new MutableLiveData<>();
    private MutableLiveData<ArrayList> last_movement_time  = new MutableLiveData<>();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private MutableLiveData<ArrayList> notifications = new MutableLiveData<>();
    private MutableLiveData<ArrayList> notifications_time = new MutableLiveData<>();

    public void setEmail(String temp)
    {
        email.setValue(temp);
    }

    public void setPassword(String temp)
    {
        password.setValue(temp);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void setFence_ids(ArrayList temp)
    {

        fence_ids.setValue(temp);

    }

    public LiveData<ArrayList> get_fence_ids() {
        return fence_ids;

    }

    public void setFence_names(ArrayList temp)
    {

        fence_names.setValue(temp);

    }

    public LiveData<ArrayList> get_fence_names() {
        return fence_names;

    }

    public void setFence_radius(ArrayList temp)
    {

        fence_radius.setValue(temp);

    }

    public LiveData<ArrayList> get_fence_radius() {
        return fence_radius;

    }

    public void setFence_center_latitude(ArrayList temp)
    {

        fence_center_latitude.setValue(temp);

    }

    public LiveData<ArrayList> get_fence_center_latitude() {
        return fence_center_latitude;

    }


    public void setFence_center_longitude(ArrayList temp)
    {

        fence_center_longitude.setValue(temp);

    }

    public LiveData<ArrayList> get_fence_center_longitude() {
        return fence_center_longitude;

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void setName(ArrayList temp)
    {

        name.setValue(temp);

    }

    public LiveData<ArrayList> get_name() {
        return name;

    }

    public void setAccount_creation_time(ArrayList temp)
    {

        account_creation_time.setValue(temp);

    }

    public LiveData<ArrayList> get_account_creation_time() {
        return account_creation_time;

    }

    public void setId_time(ArrayList temp)
    {

        id_time.setValue(temp);

    }

    public LiveData<ArrayList> get_id_time() {
        return id_time;

    }

    public void setFences(ArrayList temp)
    {

        fences.setValue(temp);

    }

    public LiveData<ArrayList> getFences() {
        return fences;

    }
    public void setEmail_info(ArrayList temp)
    {

        email_info.setValue(temp);

    }

    public LiveData<ArrayList> getEmail_info() {
        return email_info;

    }
    public void setBattery_info(ArrayList temp)
    {

        battery_info.setValue(temp);

    }

    public LiveData<ArrayList> getBattery_info() {
        return battery_info;

    }
    public void setAccuracy_info(ArrayList temp)
    {

        accuracy_info.setValue(temp);

    }

    public LiveData<ArrayList> getAccuracy_info() {
        return accuracy_info;

    }
    public void setLatitude(ArrayList temp)
    {

        latitude.setValue(temp);

    }

    public LiveData<ArrayList> getLatitude() {
        return latitude;

    }
    public void setLongitude(ArrayList temp)
    {

        longitude.setValue(temp);

    }

    public LiveData<ArrayList> getLongitude() {
        return longitude;

    }
    public void setId(ArrayList temp)
    {

        id.setValue(temp);

    }

    public LiveData<ArrayList> getId() {
        return id;

    }
    public void setLast_movement_time(ArrayList temp)
    {

        last_movement_time.setValue(temp);

    }

    public LiveData<ArrayList> getLast_movement_time() {
        return last_movement_time;

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void setNotifications(ArrayList temp)
    {

        notifications.setValue(temp);

    }

    public LiveData<ArrayList> getNotifications() {
        return notifications;

    }

    public void setNotifications_time(ArrayList temp)
    {

        notifications_time.setValue(temp);

    }

    public LiveData<ArrayList> getNotifications_time() {
        return notifications_time;

    }

}
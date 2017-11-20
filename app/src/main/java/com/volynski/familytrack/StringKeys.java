package com.volynski.familytrack;

/**
 * Created by DmitryVolynski on 23.08.2017.
 *
 * Contains all string keys of the app
 */

public class StringKeys {

    // Shared preferences keys
    public static final String SHARED_PREFS_FILE_KEY
            = StringKeys.class.getPackage().getName() + ".PREFERENCE_FILE_KEY";
    public static final String SHARED_PREFS_CURRENT_USER_KEY = "CURRENT_USER";
    public static final String SHARED_PREFS_IDTOKEN_KEY = "ID_TOKEN";
    public static final String SHARED_PREFS_CURRENT_USER_ACTIVE_GROUP_KEY = "SHARED_PREFS_CURRENT_USER_ACTIVE_GROUP";
    public static final String SHARED_PREFS_CURRENT_INTERVAL_KEY = "SHARED_PREFS_CURRENT_INTERVAL";
    public static final String SHARED_PREFS_SETTINGS_KEY = "SHARED_PREFS_SETTINGS";
    public static final String SHARED_PREFS_SIMULATED_LATLNG_KEY = "SHARED_PREFS_SIMULATED_LATLNG";
    public static final String SHARED_PREFS_GEOFENCES_KEY = "SHARED_PREFS_GEOFENCES";

    public static final String CURRENT_USER_UUID_KEY = "CURRENT_USER_UUID";
    public static final String USER_UUID_KEY = "USER_UUID";
    public static final String SHARED_PREFS_CURRENT_USER_UUID_KEY = "CURRENT_USER_UUID";

    public static final String MAIN_ACTIVITY_MODE_KEY = "MAIN_ACTIVITY_MODE";
    public static final String USER_UPDATE_RESULT_KEY = "USER_UPDATE_RESULT";
    public static final String SETTINGS_UPDATE_RESULT_KEY = "SETTINGS_UPDATE_RESULT";
    public static final String SNACKBAR_TEXT_KEY = "SNACKBAR_TEXT";
    public static final String SHARED_PREFS_CONNECTION_STATUS_KEY = "CONNECTION_STATUS";

    public static final String INVITE_USERS_LAYOUT_POSITION_KEY = "INVITE_USERS_LAYOUT_POSITION";
    public static final String INVITE_USERS_VIEWMODEL_BUNDLE_KEY = "INVITE_USERS_VIEWMODEL_BUNDLE";
    public static final String INVITE_USERS_VM_SEARCH_STRING_KEY = "INVITE_USERS_VM_SEARCH_STRING";
    public static final String INVITE_USERS_VM_SELECTED_USERS_KEY = "INVITE_USERS_VM_SELECTED_USERS";
    public static final String INVITE_USERS_DIALOG_SHOW_KEY = "INVITE_USERS_DIALOG_SHOW";

    public static final String SETTINGS_CHANGED_FLAG_KEY = "SETTINGS_CHANGED_FLAG";
    public static final String TRACKING_SERVICE_COMMAND_KEY = "TRACKING_SERVICE_COMMAND";
    public static final String FRAGMENT_ALREADY_CREATED_KEY = "FRAGMENT_ALREADY_CREATED";
    public static final String STARTED_FROM_NOTIFICATION_KEY = "STARTED_FROM_NOTIFICATION";
    public static final String FIRST_TIME_USER_DIALOG_KEY = "FIRST_TIME_USER_DIALOG";
    public static final String CREATED_FROM_CONTACTS_KEY = "---";

    public static String NEW_GROUP_NAME_KEY = "NEW_GROUP_NAME";
}

// 12.10.2017 Доделать детальную форму контакта с редактированием, сохранением и удалением
// 17.10.2017 доделать исключение клиента из группы
// 16.10.2017 реализовать экран настроек с сохранением в БД
// 16.10.2017 проверка данных при изменении настроек
// 22.10.2017 чтение и использование настроек
// реализовать левую панель навигации (Drawer)
// 23.10.2017 отображение пути и координат в динамике
// вступление/выход пользователя из группы
// 23.10.2017 механизм эмуляции движения
// 17.10.2017 добавление/удаление пользователей для geofences
// 24.10.2017 удаление группы при выходе последнего пользователя - неактуально. много работы и проверок
// 24.10.2017 добавление пользователей в процессе работы администратором
// 02.11.2017 работа с уведомлениями по geofences
// 02.11.2017 настройка geofences
// 04.11.2017 разработка виджета
// 11.11.2017 исключение пользователя группы свайпом
// 11.11.2017 работа приложения при изменении ориентации
// 14.11.2017 Изменить механизм позиционирования клиента (увеличить точность)
// 14.11.2017 Переделать позиционирование клиента под событийную модель
// 15.11.2017 адаптация форм для планшета
// 16.11.2017 запрос № телефона через API
// 18.11.2017 проверка geofences
// 20.11.2017 Material design + цветовая схема

// === функционал
// TODO: проверка ролей

// === UI
// TODO: добавить индикатор ожидания при загрузке данных
// TODO: тестирование

// License: Linkware (Backlink to http://www.alienvalley.com required)
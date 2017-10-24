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

    public static final String CURRENT_USER_UUID_KEY = "CURRENT_USER_UUID";
    public static final String USER_UUID_KEY = "USER_UUID";
    public static final String SHARED_PREFS_CURRENT_USER_UUID_KEY = "CURRENT_USER_UUID";

    public static final String MAIN_ACTIVITY_MODE_KEY = "MAIN_ACTIVITY_MODE";
    public static final String USER_UPDATE_RESULT_KEY = "USER_UPDATE_RESULT";
    public static final String SETTINGS_UPDATE_RESULT_KEY = "SETTINGS_UPDATE_RESULT";
    public static final String SNACKBAR_TEXT_KEY = "SNACKBAR_TEXT";
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

// === функционал
// TODO: Изменить механизм позиционирования клиента (увеличить точность)
// TODO: добавление пользователей в процессе работы администратором
// TODO: разработка виджета
// TODO: работа с уведомлениями по geofences
// TODO: проверка ролей

// === UI
// TODO: опция для отображения маркеров в пути + динамическое отображение пути (либо seekBar)
// TODO: адаптация форм для планшета
// TODO: Material design
// TODO: добавить индикатор ожидания при загрузке данных
// TODO: тестирование

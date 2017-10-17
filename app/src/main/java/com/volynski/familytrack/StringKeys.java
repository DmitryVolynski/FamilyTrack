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
// TODO: Изменить механизм позиционирования клиента (увеличить точность)
// 16.10.2017 реализовать экран настроек с сохранением в БД
// 16.10.2017 проверка данных при изменении настроек
// TODO: чтение и использование настроек
// реализовать левую панель навигации (Drawer)
// TODO: отображение пути и координат в динамике
// TODO: адаптация форм для планшета
// TODO: Material design
// TODO: удаление/добавление пользователей в процессе работы администратором
// TODO: удаление группы при выходе последнего пользователя
// вступление/выход пользователя из группы
// TODO: разработка виджета
// TODO: механизм эмуляции движения
// 17.10.2017 добавление/удаление пользователей для geofences
// TODO: работа с уведомлениями по geofences
// TODO: добавить индикатор ожидания при загрузке данных
// TODO: тестирование

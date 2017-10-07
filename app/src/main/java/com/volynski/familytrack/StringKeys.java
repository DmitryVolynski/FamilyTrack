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

    public static final String USER_UUID_KEY = "USER_UUID";
    public static final String SHARED_PREFS_CURRENT_USER_UUID_KEY = "CURRENT_USER_UUID";

    public static final String MAIN_ACTIVITY_MODE_KEY = "MAIN_ACTIVITY_MODE";
}

// TODO: Доделать детальную форму контакта с редактированием, сохранением и удалением
// TODO: Изменить механизм позиционирования клиента (увеличить точность)
// TODO: реализовать экран настроек с сохранением в БД
// TODO: чтение и использование настроек
// реализовать левую панель навигации (Drawer)
// TODO: отображение пути и координат в динамике
// TODO: адаптация форм для планшета
// TODO: Material design
// TODO: удаление/добавление пользователей в процессе работы администратором
// TODO: вступление/выход пользователя из группы
// TODO: разработка виджета
// TODO: механизм эмуляции движения
// TODO: добавление/удаление пользователей для geofences
// TODO: работа с уведомлениями по geofences

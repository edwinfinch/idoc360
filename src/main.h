#include <pebble.h>

Window *blks_window, *menu_window, *settings_window, *debug_window, *aboot_window;
SimpleMenuLayer *menu_layer;

GBitmap *wf_icon, *settings_icon, *log_icon, *aboot_icon, *gesture_icon;

TextLayer *aboot_edwin, *aboot_version;
InverterLayer *aboot_theme;

SimpleMenuLayer *main_menu, *settings_menu;
SimpleMenuSection menu_sections[1];
SimpleMenuItem menu_settings_items[5];
SimpleMenuSection s_menu_sections[1];
SimpleMenuItem s_menu_settings_items[2];
typedef struct Settings {
	int gesture;
	int watchface;
	bool btalerts;
	bool override;
}Settings;

Settings settings;

/*
Settings:
-Bluetooth
-Ability to override
*/
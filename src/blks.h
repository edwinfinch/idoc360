#pragma once

void unload_blks();
void load_blks();
void blks_tick(struct tm *t, TimeUnits units_changed);
void blks_bat(BatteryChargeState charge);
void blks_bt(bool connected);
void blks_tap();
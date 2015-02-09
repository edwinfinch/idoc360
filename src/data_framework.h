#pragma once

void process_tuple(Tuple *t);
void inbox(DictionaryIterator *iter, void *context);
void send_data(uint8_t data);
void logging(bool isLogging, TextLayer *layer);
bool isLogging();
void send_light_status(int on);
void gesture_fired();
#include <pebble.h>

TextLayer *text_layer_init(GRect location, GColor background, GTextAlignment alignment, int font); 
void animate_layer(Layer *layer, GRect *start, GRect *finish, int duration, int delay);
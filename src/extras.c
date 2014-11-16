#include <pebble.h>
#include "extras.h"

TextLayer *text_layer_init(GRect location, GColor background, GTextAlignment alignment, int font){
	TextLayer *layer = text_layer_create(location);
	text_layer_set_text_color(layer, GColorBlack);
	text_layer_set_background_color(layer, background);
	text_layer_set_text_alignment(layer, alignment);
	switch(font){
		case 1:
			text_layer_set_font(layer, fonts_get_system_font(FONT_KEY_GOTHIC_24_BOLD));
			break;
		case 2:
			text_layer_set_font(layer, fonts_get_system_font(FONT_KEY_GOTHIC_24));
			break;
	}
	return layer;
}

void stopped(Animation *anim, bool finished, void *context){
    property_animation_destroy((PropertyAnimation*) anim);
}
 
void animate_layer(Layer *layer, GRect *start, GRect *finish, int duration, int delay){
    PropertyAnimation *anim = property_animation_create_layer_frame(layer, start, finish);
     
    animation_set_duration((Animation*) anim, duration);
    animation_set_delay((Animation*) anim, delay);
     
    AnimationHandlers handlers = {
        .stopped = (AnimationStoppedHandler) stopped
    };
    animation_set_handlers((Animation*) anim, handlers, NULL);
     
    animation_schedule((Animation*) anim);
}
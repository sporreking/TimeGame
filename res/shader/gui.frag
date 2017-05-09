#version 300 es
precision highp float;

uniform bool b_is_fader;
uniform bool b_has_text;
uniform float f_value;
uniform vec4 v_text_color;
uniform vec4 v_hue;
uniform sampler2D t_mask;
uniform sampler2D t_text;
uniform sampler2D t_sampler;
uniform sampler2D t_sampler_on;

in highp vec2 pass_texCoords;

out highp vec4 out_color;

void main()
{
	if (b_is_fader) {
		//Is a fader
		//Check if the first channel is less than the value
		if (f_value <= texture2D(t_mask, pass_texCoords).x) {
			out_color = texture2D(t_sampler, pass_texCoords);
		} else {
			out_color = texture2D(t_sampler_on, pass_texCoords);
		}
	} else {
		//Not a fader
		out_color = texture2D(t_sampler, pass_texCoords);
	}
	
	out_color *= v_hue;

	if (b_has_text) {
		//There is text here, so it should be the text color
		out_color = mix(out_color, v_text_color, texture2D(t_text, pass_texCoords).x);
	}
}

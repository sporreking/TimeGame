#version 300 es
precision highp float;

uniform bool b_is_fader;
uniform float f_value;
uniform sampler2D t_mask;
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
	//out_color = vec4(pass_texCoords, 0, 1);
}

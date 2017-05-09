#version 300 es
precision highp float;

uniform sampler2D t_sampler;
uniform bool uses_color;
uniform vec4 in_color;

in highp vec2 pass_texCoords;

out highp vec4 out_color;

void main()
{
	//out_color = vec4(pass_texCoords, 0, 1);
	if (!uses_color) {
		out_color = texture2D(t_sampler, pass_texCoords);
	} else {
		out_color = in_color;
	}
	
}

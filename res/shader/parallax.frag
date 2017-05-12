#version 300 es
precision highp float;

uniform sampler2D t_sampler;

in highp vec2 pass_texCoords;

out highp vec4 out_color;

void main()
{
	//out_color = vec4(pass_texCoords, 0, 1);
	out_color = texture(t_sampler, pass_texCoords);
}

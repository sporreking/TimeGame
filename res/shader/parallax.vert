#version 300 es
precision highp float;

uniform highp mat4 projection;
uniform highp mat4 view;
uniform highp mat4 model;
uniform highp vec2 parallax;

layout(location=0) in highp vec2 in_pos;
layout(location=1) in highp vec2 in_texCoords;

out vec2 pass_texCoords;

void main()
{
	pass_texCoords = in_texCoords;
	vec4 pos = (projection * view * model * vec4(in_pos + parallax, 0, 1));
	pos.w = 1.0f;
	gl_Position = pos;
}

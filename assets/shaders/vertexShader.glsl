//mvp vertex shader
#version 330 core
layout (location = 0) in vec2 vertexPosition;
layout (location = 1) in vec2 vertexUV;

out vec2 fragUV;

uniform mat4 projection;
uniform mat4 view;
uniform mat4 translation;

void main() {
	
	gl_Position = projection * view * translation * vec4(vertexPosition , 0 , 1);
	fragUV = vertexUV;

}
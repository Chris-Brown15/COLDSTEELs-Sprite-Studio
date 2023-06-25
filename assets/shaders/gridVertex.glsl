#version 330 core
layout (location = 0) in vec2 inPos;

out vec2 position;

uniform mat4 projection;
uniform mat4 view;
 	
void main() {
	
	gl_Position = projection * view * vec4(inPos , 0 , 1);
	position = inPos;

}
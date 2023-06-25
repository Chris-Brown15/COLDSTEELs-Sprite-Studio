#version 330 core

uniform vec2 dims;

in vec2 position;

float plot(vec2 st) {
	
	float res;

	for(float f = 0.0 ; f < 1.0 ; f += 0.02) {

		res += smoothstep(0.0003 , 0.0 , abs(-.5 + f + st.x)) + smoothstep(0.0003 , 0.0 , abs(-0.5 + f + st.y));

	}

	return res;

}

void main() {
	
	vec2 st = position / dims;

	vec3 color = vec3(0);
	
	float res = plot(st);

	color = (1 - res) * (color + res) * vec3(1);

	gl_FragColor = vec4(color , res != 0 ? 1.0 : 0);
	
}
attribute vec2 position;
attribute float height;

uniform float terrainSize;
uniform float nodeSize;
uniform vec2 nodePos;
uniform mat4 matProjView;

varying vec2 nodeCoords;
varying vec2 groundTexCoords;

void main(void) {
    //nodeCoords = position / nodeSize; 
    
    vec3 pos = vec3(position.x + nodePos.x, position.y + nodePos.y, height);
    groundTexCoords = vec2(pos.x / terrainSize, pos.y / terrainSize);
	gl_Position = matProjView * vec4(pos, 1.0);
}

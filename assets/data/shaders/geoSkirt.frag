precision mediump float;

uniform float nodeSize;
uniform sampler2D texGround;

varying vec2 groundTexCoords;
varying vec2 nodeCoords;

void main() {
    vec3 ground = texture2D(texGround, groundTexCoords).rgb;
    vec3 color = ground;
    
    //vec3 color = vec3(nodeCoords.x, 1, nodeCoords.y);

    gl_FragColor.rgb = color;     
}
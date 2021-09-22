import {readFileSync, writeFileSync} from 'fs';

const argv = process.argv;
const src = 'src/main/resources';

if (argv.length !== 4) {
	console.error("Missing recipe ID, or too many arguments.");
	process.exit(1);
}

class Identifier
{
	constructor(namespace, path) {
		this.namespace = namespace;
		this.path = path;
	}

	toString() {
		return `${this.namespace}:${this.path}`;
	}
}

function parse_id(id) {
	let split = id.split(':');
	if (split.length === 1)
		return new Identifier('minecraft', split[0]);
	else
		return new Identifier(split[0], split[1]);
}

function id_to_path(id, prefix, ext) {
	return `${src}/data/${id.namespace}/${prefix}${id.path}.${ext}`
}

function read_recipe(id) {
	return JSON.parse(readFileSync(id_to_path(id, 'recipes/', 'json'), 'utf8'));
}

function parse_ingredients(recipe) {
	if (recipe.type === 'minecraft:crafting_shaped') {
		let ingredients = [];

		for (const [, value] of Object.entries(recipe.key)) {
			ingredients.push(value);
		}

		return ingredients;
	} else if (recipe.type === 'minecraft:crafting_shapeless') {
		return recipe.ingredients;
	} else if (recipe.type === 'minecraft:stonecutting' || recipe.type === 'aurorasdeco:woodcutting'
		|| recipe.type === 'minecraft:smelting') {
		if (recipe.ingredient instanceof Array) {
			return recipe.ingredient;
		} else return [recipe.ingredient];
	}
}

function parse_result(recipe) {
	if (recipe.type === 'minecraft:stonecutting' || recipe.type === 'aurorasdeco:woodcutting' || recipe.type === 'minecraft:smelting') {
		return recipe.result;
	} else {
		return recipe.result.item;
	}
}

function ingredient_to_advancement_criteria(ingredient) {
	if (ingredient.item !== undefined) {
		return {items: [ingredient.item]};
	} else {
		return ingredient;
	}
}

let id = parse_id(process.argv[2]);

let recipe = read_recipe(id);
let ingredients = parse_ingredients(recipe);
let result = parse_result(recipe);

let advancement_path = id_to_path(id, 'advancements/recipes/' + process.argv[3] + '/', 'json');

let advancement = {
	parent: 'minecraft:recipes/root',
	rewards: {
		recipes: [id.toString()]
	},
	criteria: {
		has_self: {
			trigger: 'minecraft:inventory_changed',
			conditions: {
				items: [
					{
						items: [
							result
						]
					}
				]
			}
		},
		has_the_recipe: {
			trigger: 'minecraft:recipe_unlocked',
			conditions: {
				recipe: id.toString()
			}
		}
	}
};

function get_criteria_name_from_ingredient(ingredient) {
	if (ingredient.tag) {
		return parse_id(ingredient.tag).path;
	} else {
		return parse_id(ingredient.item).path.replace("/", "_");
	}
}

for (const ingredient of ingredients) {
	advancement.criteria['has_' + get_criteria_name_from_ingredient(ingredient)] = {
		trigger: 'minecraft:inventory_changed',
		conditions: {
			items: [
				ingredient_to_advancement_criteria(ingredient)
			]
		}
	};
}

let criterias = [];
for (const criteria_key of Object.keys(advancement.criteria)) {
	criterias.push(criteria_key);
}

advancement.requirements = [
	criterias
];

writeFileSync(advancement_path, JSON.stringify(advancement, null, 2));
//console.log(JSON.stringify(advancement, null, 2));

//console.log(`recipe ${JSON.stringify(recipe)}, ingredients ${JSON.stringify(ingredients)}, result ${result}`);
